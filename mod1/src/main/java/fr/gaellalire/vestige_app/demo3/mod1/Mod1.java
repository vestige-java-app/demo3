package fr.gaellalire.vestige_app.demo3.mod1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;
import java.util.concurrent.Callable;

import fr.gaellalire.vestige.spi.job.DummyJobHelper;
import fr.gaellalire.vestige.spi.resolver.AttachedClassLoader;
import fr.gaellalire.vestige.spi.resolver.ResolvedClassLoaderConfiguration;
import fr.gaellalire.vestige.spi.resolver.Scope;
import fr.gaellalire.vestige.spi.resolver.maven.CreateClassLoaderConfigurationRequest;
import fr.gaellalire.vestige.spi.resolver.maven.MavenContextBuilder;
import fr.gaellalire.vestige.spi.resolver.maven.ResolveMode;
import fr.gaellalire.vestige.spi.resolver.maven.VestigeMavenResolver;

public class Mod1 implements Callable<Void> {

    private VestigeMavenResolver mavenResolver;

    @SuppressWarnings("unused")
    private File config;

    private File data;

    @SuppressWarnings("unused")
    private File cache;

    public Mod1(File config, File data, File cache) {
        this.config = config;
        this.data = data;
        this.cache = cache;
    }

    public void setMavenResolver(final VestigeMavenResolver mavenResolver) {
        this.mavenResolver = mavenResolver;
    }

    @Override
    public Void call() throws Exception {

        System.out.println("mod1");

        try {
            System.exit(0);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        long time = System.currentTimeMillis();

        ResolvedClassLoaderConfiguration classLoaderConfiguration = null;

        String osName = System.getProperty("os.name").toLowerCase();
        boolean windows = osName.contains("windows");
        boolean mac = osName.contains("mac");

        String modToLoad = null;
        File cacheModFile = null;
        String className = null;
        String pluginName = null;
        if (mac) {
            pluginName = "mod2-plugin";
            modToLoad = "demo3.mod2";
            cacheModFile = new File(data, "mod2.data");
            className = "fr.gaellalire.vestige_app.demo3.mod2.Mod2";
        } else if (!windows) {
            pluginName = "mod3-plugin";
            modToLoad = "demo3.mod3";
            cacheModFile = new File(data, "mod3.data");
            className = "fr.gaellalire.vestige_app.demo3.mod3.Mod3";
        }

        if (cacheModFile.exists()) {
            try {
                ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(cacheModFile));
                try {
                    classLoaderConfiguration = mavenResolver.restoreSavedResolvedClassLoaderConfiguration(objectInputStream);
                } finally {
                    objectInputStream.close();
                }
                System.out.println("Restored classloader conf in " + (System.currentTimeMillis() - time));
            } catch (Exception e) {

            }
        }
        if (classLoaderConfiguration == null) {
            MavenContextBuilder mavenContextBuilder = mavenResolver.createMavenContextBuilder();
            mavenContextBuilder.addAdditionalRepository("gaellalire-repo", null, "https://gaellalire.fr/maven/repository/");
            Properties demo3Properties = new Properties();
            demo3Properties.load(Mod1.class.getResourceAsStream("demo3.properties"));
            String version = demo3Properties.getProperty("version");
            CreateClassLoaderConfigurationRequest createClassLoaderConfiguration = mavenContextBuilder.build().resolve("fr.gaellalire.vestige_app.demo3", modToLoad, version)
                    .execute(DummyJobHelper.INSTANCE).createClassLoaderConfiguration(pluginName, ResolveMode.FIXED_DEPENDENCIES, Scope.PLATFORM);
            createClassLoaderConfiguration.setNamedModuleActivated(true);
            classLoaderConfiguration = createClassLoaderConfiguration.execute();

            System.out.println("Created classloader conf in " + (System.currentTimeMillis() - time));

            ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(cacheModFile));
            try {
                classLoaderConfiguration.save(objectOutputStream);
            } finally {
                objectOutputStream.close();
            }
        }

        AttachedClassLoader attach = classLoaderConfiguration.attach();
        try {
            Plugin p = (Plugin) attach.getAttachableClassLoader().getClassLoader().loadClass(className).getConstructor().newInstance();
            p.doSomething();
        } finally {
            System.out.println("finally in mod1");
            attach.detach();
        }
        return null;
    }
}
