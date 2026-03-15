# Jetski Async Script Runner App v1.0.0
Coded by av Oskar Bergström 2026-03-15

## Howto 

### compile
```bash
  mvn clean package
```

### run:
```bash
  java -jar target/jetski.jar
```

Settings

You can edit the app settings in two ways
1) Start the app and go to "File" -> "Settings" and edit, click "Save", done.
2) Open the settings.yml directly in your favourite text editor, and change it.

3) settings.yml struktur:

#### settings.yml

```yaml
scripts:
- name: 'Demo'
  path: './myscript.sh'
  parameters: 'param1 param2 "param3 multi param", param4'
  tooltip: 'Script that demonstrate how params is sent!'
- name: 'Personnummer'
  path: 'pnr'
  parameters: '-xj 19121212-1212'
  tooltip: 'Script that call the Pnr command, directly, using params'
```


