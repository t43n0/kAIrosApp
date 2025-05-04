import dotenv from 'dotenv';
import https from 'https';
import fs from 'fs';
import app from './app.js';

dotenv.config();

const options = {
  key: fs.readFileSync('key.pem'),
  cert: fs.readFileSync('cert.pem'),
};

const PORT = process.env.PORT || 3001;

https.createServer(options, app).listen(PORT, () => {
  console.log(`✅ Servidor HTTPS activo en https://localhost:${PORT}`);
});
