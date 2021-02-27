package fr.gaellalire.vestige_app.demo3.mod3;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import fr.gaellalire.vestige_app.demo3.mod1.Plugin;

public class Mod3 implements Plugin {

    @Override
    public void doSomething() {
        System.out.println("doSomething from Mod3");

        Display.setAppName("Hello");
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setLayout(new FillLayout(SWT.VERTICAL));
        shell.setText("Hello Linux World");
        Label label = new Label(shell, SWT.CENTER);
        label.setText("Hello World");
        shell.setSize(300, 300);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                // ctrl-C here will not interrupt before sleep returns ...
                display.sleep();
            }
        }
        display.dispose();

        synchronized (this) {
            System.out.println("waiting to die");
            try {
                this.wait();
            } catch (InterruptedException e) {
                System.out.println("end of mod2");
            }
        }
    }

}
