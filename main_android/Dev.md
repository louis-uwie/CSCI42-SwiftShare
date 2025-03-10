# **SwiftShare Development Guidelines**

**Since this is going to be a development with more than 3 people involved, it is best to have a proper convention throughout the project.**

### **Naming Convention**
- **Consistency** is key: Use meaningful, descriptive names for files, variables, methods, and classes.
- **CamelCase** should be used for method and variable names (e.g., `getUserData()`, `fileTransferStatus`).
- **PascalCase** should be used for class and interface names (e.g., `BluetoothManager`, `FileTransferService`).
- **Snake_case** is acceptable for filenames when appropriate (e.g., `file_transfer_activity.xml`).
- **Use Singular for Class Names**: Classes should have singular names (e.g., `BluetoothManager` instead of `BluetoothManagers`).

## **Project Structure**

### **Android Project**

```
/android
│
├── /Bluetooth           # Bluetooth-specific logic (discovery, transfer)
├── /Lan                 # LAN-related logic (file transfer over local network)
├── /Ui                  # UI-related components (activities, fragments)
├── /res                 # Resources (layout, images, strings)
└── AndroidManifest.xml
```

### **Desktop (JavaFX) Project**

```
/desktop
│
├── /bluetooth           # Bluetooth logic (discovery, file transfer)
├── /lan                 # LAN-based transfer (client/server)
├── /ui                  # JavaFX UI components (screens, controllers)
├── /resources           # Resources (styles, images)
└── pom.xml
```

## **Commit Message Guidelines**

- **Structure**: Use the format:
  ```
  [Type]: [Short description of changes]
  ```
- **Types**: 
  - **feat**: New feature
  - **fix**: Bug fix
  - **docs**: Documentation update
  - **style**: Code style changes (whitespace, formatting)
  - **refactor**: Code refactor (no feature change)
  - **test**: Adding tests or modifying tests
  - **chore**: Miscellaneous tasks (build, configuration)