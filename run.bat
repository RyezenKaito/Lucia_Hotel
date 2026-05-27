@echo off
echo Dang khoi chay Lucia Hotel...
"C:\Program Files\Java\jdk-22\bin\java.exe" -Djava.library.path=dl --module-path "C:\Program Files\Java\javafx-sdk-21.0.11\lib;lib" --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.media,javafx.swing,javafx.web,atlantafx.base -cp "bin;lib/*" main.App
pause
