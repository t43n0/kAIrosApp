import { setGlobalOptions } from "firebase-functions/v2/options";
import * as logger from "firebase-functions/logger";

setGlobalOptions({
  region: "europe-west1",
  maxInstances: 10,
  memory: "512MiB",
  timeoutSeconds: 60
});

export { generateImage } from "./src/generateImage.js";
export { analyzeWeek } from "./src/analyzeWeek.js";

logger.info("[Functions] Endpoints cargados: generateImage, analyzeWeek");