# **SwiftShare Development Guidelines**

**Since this is going to be a development with more than 3 people involved, it is best to have a proper convention throughout the project.**

## **Naming Conventions**

### **General Guidelines**
- **Consistency** is key: Use meaningful, descriptive names for files, variables, methods, and classes.
- **CamelCase** should be used for method and variable names (e.g., `getUserData()`, `fileTransferStatus`).
- **PascalCase** should be used for class and interface names (e.g., `BluetoothManager`, `FileTransferService`).
- **Snake_case** is acceptable for filenames when appropriate (e.g., `file_transfer_activity.xml`).
- **Use Singular for Class Names**: Classes should have singular names (e.g., `BluetoothManager` instead of `BluetoothManagers`).

### **Android Specific Naming Conventions**

1. **Activities**:
   - Name your activity classes with the suffix `Activity`. 
   - Example: `MainActivity`, `FileSelectionActivity`.
   
2. **Fragments**:
   - Name fragment classes with the suffix `Fragment`.
   - Example: `BluetoothFragment`, `FileTransferFragment`.

3. **Layouts**:
   - Layout XML files should use **snake_case** and be descriptive.
   - Example: `activity_main.xml`, `fragment_bluetooth.xml`.

4. **Resources**:
   - **String resources**: Use `lowercase_with_underscores` for resource names.
   - Example: `file_transfer_success_message`, `bluetooth_error_message`.
   
5. **Adapters**:
   - Name adapter classes with the suffix `Adapter`.
   - Example: `RecipeAdapter`, `BluetoothDeviceAdapter`.

### **JavaFX (Desktop) Specific Naming Conventions**

1. **Java Classes**:
   - Use **PascalCase** for class names.
   - Example: `FileTransferManager`, `BluetoothHandler`.

2. **FXML Files**:
   - Name FXML files using **snake_case** with a descriptive name.
   - Example: `main_screen.fxml`, `file_transfer_screen.fxml`.

3. **Controllers**:
   - Controller classes should follow the format `ClassNameController` (e.g., `FileTransferController`, `MainScreenController`).

4. **CSS Files**:
   - Use **kebab-case** for CSS file names.
   - Example: `main_screen.css`, `file_transfer_style.css`.

### **Variables and Functions**

- **Variables**: Use meaningful names with **camelCase**. Avoid abbreviations unless they are widely recognized.
  - Example: `userName`, `bluetoothDeviceList`.

- **Functions**: Use verbs or verb phrases in function names, and follow **camelCase**.
  - Example: `startFileTransfer()`, `getAvailableDevices()`.

- **Constants**: Constants should be in **UPPER_SNAKE_CASE**.
  - Example: `MAX_FILE_SIZE`, `BLUETOOTH_DEVICE_TIMEOUT`.

### **Bluetooth and LAN Logic Naming**

1. **Bluetooth**:
   - Use `Bluetooth` in class names, variables, and methods related to Bluetooth.
   - Example: `BluetoothDiscovery`, `BluetoothFileSender`, `bluetoothConnectionStatus`.

2. **LAN**:
   - Use `Lan` in class names, variables, and methods related to LAN.
   - Example: `LanServer`, `LanFileTransferManager`, `clientLanSocket`.

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

- **Examples**:
  - `feat: Implement Bluetooth file transfer functionality`
  - `fix: Resolve issue with Bluetooth connection stability`
  - `docs: Update README for developer setup`

## **Quick Reminders for Developers**

- **Branches**: Always branch out from `main` before working on a new feature or bug fix.
  - Example: `git checkout -b feature/bluetooth-connection`
  
- **Testing**: Ensure that all features are tested across both Android and desktop platforms.
  - Unit tests are essential for the backend logic (Bluetooth, LAN).
  - UI testing is crucial for both Android and JavaFX (use tools like Espresso for Android).

- **Documentation**: Keep documentation up to date with each new feature or change. Add notes on any technical or architectural decisions made.
  - Always include detailed descriptions in **`dev.md`**.

- **Consistency**: Stick to the naming conventions above, and ensure code readability by adhering to standard code style guidelines.
  
- **File Transfers**: When dealing with file transfers:
  - Validate file sizes before transfer.
  - Handle errors such as timeout and disconnection gracefully.
  
- **Code Reviews**: Always participate in code reviews to ensure that code quality and conventions are maintained. Reviewers should focus on:
  - Code readability.
  - Correctness of the implementation.
  - Consistency with the project structure and naming conventions.