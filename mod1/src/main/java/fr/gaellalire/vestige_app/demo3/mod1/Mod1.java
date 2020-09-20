package fr.gaellalire.vestige_app.demo3.mod1;

import java.util.Properties;
import java.util.concurrent.Callable;

import fr.gaellalire.vestige.spi.job.DummyJobHelper;
import fr.gaellalire.vestige.spi.resolver.AttachedClassLoader;
import fr.gaellalire.vestige.spi.resolver.ResolvedClassLoaderConfiguration;
import fr.gaellalire.vestige.spi.resolver.Scope;
import fr.gaellalire.vestige.spi.resolver.maven.MavenContextBuilder;
import fr.gaellalire.vestige.spi.resolver.maven.ResolveMavenArtifactRequest;
import fr.gaellalire.vestige.spi.resolver.maven.ResolveMode;
import fr.gaellalire.vestige.spi.resolver.maven.VestigeMavenResolver;

public class Mod1 implements Callable<Void> {

    private VestigeMavenResolver mavenResolver;

    public void setMavenResolver(final VestigeMavenResolver mavenResolver) {
        this.mavenResolver = mavenResolver;
    }

    @Override
    public Void call() throws Exception {
        MavenContextBuilder mavenContextBuilder = mavenResolver.createMavenContextBuilder();
        mavenContextBuilder.addAdditionalRepository("gaellalire-repo", null, "https://gaellalire.fr/nexus/content/repositories/releases/");
        Properties demo3Properties = new Properties();
        demo3Properties.load(Mod1.class.getResourceAsStream("demo3.properties"));
        String version = demo3Properties.getProperty("version");
        ResolveMavenArtifactRequest request = mavenContextBuilder.build().resolve(ResolveMode.FIXED_DEPENDENCIES, Scope.PLATFORM, "fr.gaellalire.vestige_app.demo3",
                "demo3.mod2", version, "mod2-plugin");
        request.setNamedModuleActivated(true);
        ResolvedClassLoaderConfiguration classLoaderConfiguration = request.execute(DummyJobHelper.INSTANCE);
        AttachedClassLoader attach = classLoaderConfiguration.attach();
        try {
            Plugin p = (Plugin) attach.getAttachableClassLoader().getClassLoader().loadClass("fr.gaellalire.vestige_app.demo3.mod2.Mod2").getConstructor().newInstance();
            p.doSomething();
        } finally {
            attach.detach();
        }
        return null;
    }
}
