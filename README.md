# **CSCI 42 - SwiftShare**
This repository hosts the code for the **CSCI 42 Project Deliverable**: a cross-platform file manager system developed using **JavaFX** (for desktop) and **Android SDK** (for mobile). The system facilitates seamless file transfers and aims to include features such as file conversion and compression.

## **CSCI 42 - SwiftShare - Introduction to Software Engineering**

### **Authors:**
- Bautista, Ronald Francis D. [placeholder](https://github.com)
- Binwag, Louis G. III - [louis-uwie](https://github.com/louis-uwie)
- Cacacho, Jean Maximus C. [jeanmaxcacacho](https://github.com/jeanmaxcacacho)
- Magtipon, Ciana Louisse G. - [cianamagtipon](https://github.com/cianamagtipon)
- Ozo, Kyle Joshua A. - [kjozo](https://github.com/kjozo)
- Zabala, Paco Antonio V. - [pacozabala](https://github.com/Pacozabala)

---

### **Before You Start, Please Branch Out from Main.**

---

## **Project Setup**

### **1. Clone the Repository**
To get started, clone the repository into your local directory:
```bash
git clone <repository_url>
```

Navigate into the project folder:
```bash
cd repository
```

### **2. Create & Activate a Virtual Environment**
If you're working with Python for any setup tasks or documentation generation:
```bash
python -m venv myenv
```

Activate the virtual environment:
- **Windows:**
  ```bash
  myenv\Scripts\activate
  ```
- **Mac/Linux:**
  ```bash
  source myenv/bin/activate
  ```

### **3. Install Dependencies**
Upgrade `pip` and install any project dependencies:
```bash
pip install --upgrade pip
pip install -r requirements.txt
```

---

## **Projected File Layout (Tentative)**

```
/SwiftShare
│
├── /android
│
├── /Bluetooth          
│   ├── BluetoothActivity.java
│   ├── BluetoothFileSender.java
│   └── BluetoothFileReceiver.java
│
├── /Lan            
│   ├── FileClient.java
│   └── FileServer.java
│
├── /Ui     
│   ├── MainActivity.java
│   ├── FileSelectionActivity.java
│   └── SettingsActivity.java
│
├── /res
│   ├── /layout
│   ├── /drawable
│   ├── /values
│   │   └── strings.xml
│
└── AndroidManifest.xml
│
├── /desktop (JAVAFX)
│   ├── /bluetooth
│   │   ├── BluetoothDiscovery.java
│   │   ├── BluetoothConnector.java
│   │   └── BluetoothFileTransfer.java
│   ├── /lan
│   │   ├── FileClient.java
│   │   ├── FileServer.java
│   ├── /ui
│   │   ├── MainApp.java
│   │   ├── FileSelection.java
│   │   └── TransferProgress.java
│   ├── /resources
│   │   ├── /images
│   │   └── /styles
│   ├── pom.xml
│
├── /documentations
│   ├── /design
│   ├── /requirements
│   ├── /iterations
│   ├── /architecture.pdf
│   ├── /system_overview.pdf
│
├── .gitignore
├── README.md
├── LICENSE
```

### **Android Project (Android Studio)**:
- **/android/SwiftShare**: Main directory for Android-specific components.
- **/Bluetooth**: Contains Java classes for Bluetooth file sending/receiving.
- **/Lan**: Contains Java classes for LAN-based file transfer.
- **/Ui**: Contains UI components like activities and settings for the Android app.
- **/res**: Resources for layouts, drawables, and values such as strings.

### **Desktop Project (JavaFX)**:
- **/desktop**: Contains Java classes for Bluetooth and LAN file transfer, UI components for JavaFX.
  - **/bluetooth**: Handles Bluetooth discovery and file transfer.
  - **/lan**: Handles LAN file transfer (client/server logic).
  - **/ui**: JavaFX components for UI rendering and file selection.
  - **/resources**: Stores images and styles for JavaFX.
  - **pom.xml**: For dependency management using Maven.

---

## **Next Steps**

1. **Create a New Branch**: Before making any changes, please branch out from the `main` branch.
   ```bash
   git checkout -b <your-branch-name>
   ```

2. **Development**: Follow the project's modular structure and begin working on the relevant Bluetooth, LAN, or UI components depending on your tasks.

3. **Documentation**: Ensure you update the documentation folder as needed with design documents and iteration reports.

---

Feel free to adjust the layout or dependencies as needed as the project progresses. Let me know if you need more specific instructions for particular steps or any help with coding tasks!