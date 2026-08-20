package net.momirealms.craftengine.core.plugin.text.minimessage;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.core.plugin.context.ContextRandoms;
import net.momirealms.craftengine.core.plugin.context.number.*;
import net.momirealms.craftengine.core.plugin.context.text.StringTag;
import net.momirealms.craftengine.core.util.random.RandomUtils;
import net.momirealms.sparrow.message.ParsingException;
import net.momirealms.sparrow.message.tag.Tag;
import net.momirealms.sparrow.message.tag.resolver.ArgumentQueue;
import net.momirealms.sparrow.message.tag.resolver.StaticTagResolver;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.TimeUnit;

public final class RandomTag extends StaticTagResolver implements StringTag {
    public static final RandomTag INSTANCE = new RandomTag();
    private static final Cache<String, NumberProvider> PROVIDER_CACHE = Caffeine.newBuilder()
            .maximumSize(256)
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build();
    private static final Map<String, DistributionFactory> DISTRIBUTIONS = new HashMap<>();

    static {
        // 位置参数顺序与各 NumberProvider 构造参数一致，带 [] 的可省略
        DISTRIBUTIONS.put("fixed", args -> {
            require(args, 1, 1, "fixed:value");
            return ConstantNumberProvider.constant(args[0]);
        });
        DISTRIBUTIONS.put("constant", DISTRIBUTIONS.get("fixed"));
        DISTRIBUTIONS.put("uniform", args -> {
            require(args, 2, 2, "uniform:min:max");
            checkMinMax(args[0], args[1]);
            return new UniformNumberProvider(ConstantNumberProvider.constant(args[0]), ConstantNumberProvider.constant(args[1]));
        });
        DISTRIBUTIONS.put("triangle", args -> {
            require(args, 2, 3, "triangle:min:max:[mode]");
            checkMinMax(args[0], args[1]);
            double mode = args.length > 2 ? args[2] : (args[0] + args[1]) / 2.0;
            return new TriangleNumberProvider(args[0], args[1], mode);
        });
        DistributionFactory normal = args -> {
            require(args, 2, 5, "normal:min:max:[mean]:[std_dev]:[max_attempts]");
            checkMinMax(args[0], args[1]);
            double mean = args.length > 2 ? args[2] : (args[0] + args[1]) / 2.0;
            double stdDev = args.length > 3 ? args[3] : (args[1] - args[0]) / 6.0;
            int maxAttempts = args.length > 4 ? (int) args[4] : 64;
            return new GaussianNumberProvider(args[0], args[1], mean, stdDev, maxAttempts);
        };
        DISTRIBUTIONS.put("normal", normal);
        DISTRIBUTIONS.put("gaussian", normal);
        DISTRIBUTIONS.put("log_normal", args -> {
            require(args, 2, 5, "log_normal:min:max:[location]:[scale]:[max_attempts]");
            double min = Math.max(args[0], 1e-6);
            double max = args[1];
            checkMinMax(min, max);
            // 默认假设 min/max 覆盖对数域 ±3σ，与 config 工厂一致
            double logMin = Math.log(min);
            double logMax = Math.log(max);
            double location = args.length > 2 ? args[2] : (logMin + logMax) / 2.0;
            double scale = args.length > 3 ? args[3] : (logMax - logMin) / 6.0;
            int maxAttempts = args.length > 4 ? (int) args[4] : 64;
            return new LogNormalNumberProvider(min, max, location, scale, maxAttempts);
        });
        DISTRIBUTIONS.put("skew_normal", args -> {
            require(args, 3, 6, "skew_normal:min:max:skewness:[mean]:[std_dev]:[max_attempts]");
            checkMinMax(args[0], args[1]);
            double mean = args.length > 3 ? args[3] : (args[0] + args[1]) / 2.0;
            double stdDev = args.length > 4 ? args[4] : (args[1] - args[0]) / 6.0;
            int maxAttempts = args.length > 5 ? (int) args[5] : 64;
            return new SkewNormalNumberProvider(args[0], args[1], mean, stdDev, args[2], maxAttempts);
        });
        DISTRIBUTIONS.put("binomial", args -> {
            require(args, 2, 2, "binomial:trials:probability");
            return new BinomialNumberProvider(ConstantNumberProvider.constant(args[0]), ConstantNumberProvider.constant(args[1]));
        });
        DISTRIBUTIONS.put("exponential", args -> {
            require(args, 1, 3, "exponential:mean:[min]:[max]");
            double min = args.length > 1 ? args[1] : 0;
            double max = args.length > 2 ? args[2] : Double.MAX_VALUE;
            checkMinMax(min, max);
            if (args[0] <= 0) throw new IllegalArgumentException("mean must be greater than 0");
            return new ExponentialNumberProvider(min, max, 1.0 / args[0], 64);
        });
        DISTRIBUTIONS.put("beta", args -> {
            require(args, 0, 4, "beta:[min]:[max]:[alpha]:[beta]");
            double min = args.length > 0 ? args[0] : 0;
            double max = args.length > 1 ? args[1] : 1;
            double alpha = args.length > 2 ? args[2] : 2;
            double beta = args.length > 3 ? args[3] : 2;
            return new BetaNumberProvider(min, max, alpha, beta);
        });
    }

    private RandomTag() {
        super("random");
    }

    public static void clearCaches() {
        PROVIDER_CACHE.invalidateAll();
    }

    public static NumberProvider getProvider(String type, List<String> params) {
        return PROVIDER_CACHE.get(type + ":" + String.join(":", params), k -> parse(type, params));
    }

    private static NumberProvider parse(String type, List<String> params) {
        DistributionFactory factory = DISTRIBUTIONS.get(type);
        if (factory != null) {
            return factory.create(toDoubles(params));
        }
        if (params.isEmpty()) {
            if (type.contains("~")) {
                String[] split = type.split("~", 2);
                double min = parseDouble(split[0]);
                double max = parseDouble(split[1]);
                checkMinMax(min, max);
                return new UniformNumberProvider(ConstantNumberProvider.constant(min), ConstantNumberProvider.constant(max));
            }
            try {
                return ConstantNumberProvider.constant(Double.parseDouble(type));
            } catch (NumberFormatException ignored) {
            }
        }
        throw new IllegalArgumentException("Unknown random distribution: " + type);
    }

    private static double[] toDoubles(List<String> params) {
        double[] args = new double[params.size()];
        for (int i = 0; i < args.length; i++) {
            args[i] = parseDouble(params.get(i));
        }
        return args;
    }

    private static double parseDouble(String s) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number: " + s);
        }
    }

    private static void require(double[] args, int min, int max, String usage) {
        if (args.length < min || args.length > max) {
            throw new IllegalArgumentException("Usage: <random:id:" + usage + ">");
        }
    }

    private static void checkMinMax(double min, double max) {
        if (min >= max) {
            throw new IllegalArgumentException("min must be less than max");
        }
    }

    @Override
    public Tag resolve(@NotNull String name, @NotNull ArgumentQueue arguments, @NotNull net.momirealms.sparrow.message.Context ctx) throws ParsingException {
        String id = arguments.popOr("No random id provided").toString();
        net.momirealms.craftengine.core.plugin.context.Context context = ctx.target() instanceof net.momirealms.craftengine.core.plugin.context.Context c ? c : null;
        final double value;
        if (!arguments.hasNext()) {
            value = context != null ? ContextRandoms.getOrRoll(context, id) : RandomUtils.generateRandomDouble(0, 1);
        } else {
            String type = arguments.pop().toString();
            List<String> params = new ArrayList<>(4);
            while (arguments.hasNext()) {
                params.add(arguments.pop().toString());
            }
            final NumberProvider provider;
            try {
                provider = getProvider(type, params);
            } catch (final RuntimeException e) {
                throw ctx.newException("Invalid random distribution: " + type, e, arguments);
            }
            value = context != null
                    ? ContextRandoms.getOrRoll(context, id, () -> provider.getDouble(context))
                    : provider.getDouble();
        }
        return Tag.selfClosingInserting(Component.text(value));
    }

    @Override
    public String resolve(String[] args, net.momirealms.craftengine.core.plugin.context.Context context) {
        String id = StringTag.requireArg(args, 0, "No random id provided");
        final double value;
        if (args.length == 1) {
            value = context != null ? ContextRandoms.getOrRoll(context, id) : RandomUtils.generateRandomDouble(0, 1);
        } else {
            NumberProvider provider = getProvider(args[1], Arrays.asList(args).subList(2, args.length));
            value = context != null
                    ? ContextRandoms.getOrRoll(context, id, () -> provider.getDouble(context))
                    : provider.getDouble();
        }
        return String.valueOf(value);
    }

    @Override
    public StringTag precompile(String[] args) {
        final String id = StringTag.requireArg(args, 0, "No random id provided");
        if (args.length == 1) {
            return (boundArgs, context) -> String.valueOf(context != null
                    ? ContextRandoms.getOrRoll(context, id)
                    : RandomUtils.generateRandomDouble(0, 1));
        }
        final NumberProvider provider = getProvider(args[1], Arrays.asList(args).subList(2, args.length));
        return (boundArgs, context) -> String.valueOf(context != null
                ? ContextRandoms.getOrRoll(context, id, () -> provider.getDouble(context))
                : provider.getDouble());
    }

    @FunctionalInterface
    private interface DistributionFactory {
        NumberProvider create(double[] args);
    }
}
