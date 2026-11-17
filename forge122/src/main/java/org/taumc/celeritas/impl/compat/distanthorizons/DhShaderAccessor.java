package org.taumc.celeritas.impl.compat.distanthorizons;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.BooleanSupplier;
import java.util.function.ToIntFunction;

/** Connects DH's optional shader-loader interface without linking its core at startup. */
public final class DhShaderAccessor implements InvocationHandler {
    private static final String ACCESSOR = "com.seibel.distanthorizons.core.wrapperInterfaces.modAccessor.IIrisAccessor";
    private static final String MOD_ACCESSOR = "com.seibel.distanthorizons.core.wrapperInterfaces.modAccessor.IModAccessor";
    private static final String INJECTOR = "com.seibel.distanthorizons.core.dependencyInjection.ModAccessorInjector";

    private final BooleanSupplier shadersActive;
    private final BooleanSupplier shadowPass;
    private final ToIntFunction<Object> depthTexture;

    private DhShaderAccessor(BooleanSupplier shadersActive, BooleanSupplier shadowPass,
                             ToIntFunction<Object> depthTexture) {
        this.shadersActive = shadersActive;
        this.shadowPass = shadowPass;
        this.depthTexture = depthTexture;
    }

    /** Must run inside DH's initializeModCompat, before it caches its accessors. */
    public static boolean register(ClassLoader loader, BooleanSupplier shadersActive,
                                   BooleanSupplier shadowPass, ToIntFunction<Object> depthTexture)
            throws ReflectiveOperationException {
        Class<?> accessorType = Class.forName(ACCESSOR, true, loader);
        Class<?> injectorType = Class.forName(INJECTOR, true, loader);
        Object injector = injectorType.getField("INSTANCE").get(null);
        if (injectorType.getMethod("get", Class.class).invoke(injector, accessorType) != null) {
            return false;
        }

        Object accessor = Proxy.newProxyInstance(loader, new Class<?>[]{accessorType},
                new DhShaderAccessor(shadersActive, shadowPass, depthTexture));
        injectorType.getMethod("bind", Class.class, Class.forName(MOD_ACCESSOR, true, loader))
                .invoke(injector, accessorType, accessor);
        return true;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        return switch (method.getName()) {
            case "getModName" -> "Pintonium";
            case "isShaderPackInUse" -> shadersActive.getAsBoolean();
            case "isRenderingShadowPass" -> shadowPass.getAsBoolean();
            case "isReverseZDuringShaders" -> false;
            case "getFramebufferDepthTextureId" -> depthTexture.applyAsInt(args[0]);
            // DH's IBindable defaults: this accessor has no delayed dependencies.
            case "finishDelayedSetup" -> null;
            case "getDelayedSetupComplete" -> true;
            case "equals" -> proxy == args[0];
            case "hashCode" -> System.identityHashCode(proxy);
            case "toString" -> "Pintonium Distant Horizons shader accessor";
            default -> throw new UnsupportedOperationException("Unsupported DH shader accessor method: " + method);
        };
    }
}
