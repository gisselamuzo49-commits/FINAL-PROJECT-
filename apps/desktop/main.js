const { app, BrowserWindow } = require("electron");
const path = require("path");

const QA_URL = "http://gisselamuzoqa1.distribuidauce.org";
const PROD_URL = "http://gissleamuzoprod1.distribuidauce.org";
const APP_URL = process.env.APP_ENV === "prod" ? PROD_URL : QA_URL;

function createWindow() {
  const win = new BrowserWindow({
    width: 1280,
    height: 800,
    minWidth: 1024,
    minHeight: 600,
    title: "Sistema de Pasantías UCE",
    webPreferences: {
      nodeIntegration: false,
      contextIsolation: true,
      preload: path.join(__dirname, "preload.js"),
    },
    icon: path.join(__dirname, "assets", "icon.png"),
  });

  win.loadURL(APP_URL).catch(() => {
    win.loadFile(path.join(__dirname, "offline.html"));
  });

  win.webContents.on("did-fail-load", () => {
    win.loadFile(path.join(__dirname, "offline.html"));
  });

  win.setMenuBarVisibility(false);
}

app.whenReady().then(() => {
  createWindow();
  app.on("activate", () => {
    if (BrowserWindow.getAllWindows().length === 0) createWindow();
  });
});

app.on("window-all-closed", () => {
  if (process.platform !== "darwin") app.quit();
});
