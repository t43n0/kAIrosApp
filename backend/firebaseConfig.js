import { initializeApp, cert } from 'firebase-admin/app';
import { getStorage } from 'firebase-admin/storage';
import dotenv from 'dotenv';
import path from 'path';
import { fileURLToPath } from 'url';

dotenv.config();

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const serviceAccountPath = path.join(__dirname, process.env.FIREBASE_PRIVATE_KEY_PATH);

initializeApp({
  credential: cert(serviceAccountPath),
  storageBucket: process.env.FIREBASE_STORAGE_BUCKET,
});

const bucket = getStorage().bucket();

export { bucket };
