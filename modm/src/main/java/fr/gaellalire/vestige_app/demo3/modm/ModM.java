package fr.gaellalire.vestige_app.demo3.modm;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
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
import fr.gaellalire.vestige.spi.trust.Signature;
import fr.gaellalire.vestige.spi.trust.TrustSystemAccessor;
import fr.gaellalire.vestige_app.demo3.mod1.Plugin;

public class ModM implements Callable<Void> {

    private static boolean trustedPlugin = false;

    private TrustSystemAccessor trustSystemAccessor;

    private VestigeMavenResolver mavenResolver;

    @SuppressWarnings("unused")
    private File config;

    private File data;

    @SuppressWarnings("unused")
    private File cache;

    public ModM(File config, File data, File cache) {
        this.config = config;
        this.data = data;
        this.cache = cache;
    }

    public void setTrustSystemAccessor(TrustSystemAccessor trustSystemAccessor) {
        this.trustSystemAccessor = trustSystemAccessor;
    }

    public void setMavenResolver(final VestigeMavenResolver mavenResolver) {
        this.mavenResolver = mavenResolver;
    }

    @Override
    public Void call() throws Exception {

        System.out.println("modm");

        try {
            // test that security is enabled
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
        String verifyFileName = null;
        if (mac) {
            pluginName = "mod2-plugin";
            modToLoad = "demo3.mod2";
            cacheModFile = new File(data, "mod2.data");
            className = "fr.gaellalire.vestige_app.demo3.mod2.Mod2";
            verifyFileName = "verify-mod2.txt";
        } else if (windows) {
            pluginName = "mod4-plugin";
            modToLoad = "demo3.mod4";
            cacheModFile = new File(data, "mod4.data");
            className = "fr.gaellalire.vestige_app.demo3.mod4.Mod4";
            verifyFileName = "verify-mod4.txt";
        } else {
            // linux
            pluginName = "mod3-plugin";
            modToLoad = "demo3.mod3";
            cacheModFile = new File(data, "mod3.data");
            className = "fr.gaellalire.vestige_app.demo3.mod3.Mod3";
            verifyFileName = "verify-mod3.txt";
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
            demo3Properties.load(ModM.class.getResourceAsStream("demo3.properties"));
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

        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(ModM.class.getResourceAsStream(verifyFileName), "UTF-8"));
        String readLine = br.readLine();
        while (readLine != null) {
            stringBuilder.append(readLine);
            stringBuilder.append('\n');
            readLine = br.readLine();
        }
        String metadata = stringBuilder.toString();

        if (trustedPlugin) {
            // TODO better signature support
            Signature signature = trustSystemAccessor.getPGPTrustSystem().loadSignature(ModM.class.getResourceAsStream(verifyFileName + ".sig"));
            if (signature.getPublicPart().isTrusted()) {
                if (signature.verify(new ByteArrayInputStream(metadata.getBytes("UTF-8")))) {
                    System.out.println("Signature OK");
                } else {
                    System.out.println("Signature KO");
                }
            } else {
                System.out.println("Not trusted");
            }
        }
        AttachedClassLoader attach = classLoaderConfiguration.verifiedAttach(metadata);

        try {
            Plugin p = (Plugin) attach.getAttachableClassLoader().getClassLoader().loadClass(className).getConstructor().newInstance();
            p.doSomething();
        } finally {
            System.out.println("finally in modm");
            attach.detach();
        }
        return null;
    }
}
