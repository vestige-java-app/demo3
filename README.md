# Demo3

This project show an example of using vestige (<https://gaellalire.fr/vestige>) in a desktop application using SWT

## Compilation

Run `mvn clean install` using a JDK 8 or before

## Installation

Unzip one of the :
-  [assemblies/vestige/target/demo3.assemblies.vestige-1.0.0-unix.zip](https://gaellalire.fr/vestige/app/demo3/demo3.assemblies.vestige-1.0.0-unix.zip)
-  [assemblies/vestige/target/demo3.assemblies.vestige-1.0.0-windows.zip](https://gaellalire.fr/vestige/app/demo3/demo3.assemblies.vestige-1.0.0-unix.zip)

## Run

Run `./demo3` with a JDK 8 or after

A log will be printed

```
11:45:21.102 ERROR -> java.security.AccessControlException: access denied ("java.lang.RuntimePermission" "exitVM.0")
```

A window will appear, close it

```
11:45:24.345 INFO -> waiting to die
```

You can now type ctrl-C

```
11:45:26.657 INFO -> Vestige SAL stopped
```