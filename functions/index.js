const onRequest = require("firebase-functions/v2/https").onRequest;
const setGlobalOptions = require("firebase-functions/v2").setGlobalOptions;
const defineSecret = require("firebase-functions/params").defineSecret;
const admin = require("firebase-admin");

if (!admin.apps.length) {
  admin.initializeApp();
}

// Región global para TODAS las funciones
setGlobalOptions({
  region: "europe-west1",
});

var OPENAI_API_KEY = defineSecret("OPENAI_API_KEY");

var generateImageModule = require("./src/generateImage");
var analyzeWeekModule = require("./src/analyzeWeek");
var deleteImageModule = require("./src/deleteImage");

exports.generateImage = onRequest(
  { secrets: [OPENAI_API_KEY] },
  function (req, res) {
    var apiKey = OPENAI_API_KEY.value();
    return generateImageModule.handler(req, res, apiKey);
  }
);

exports.analyzeWeek = onRequest(
  { secrets: [OPENAI_API_KEY] },
  function (req, res) {
    var apiKey = OPENAI_API_KEY.value();
    return analyzeWeekModule.handler(req, res, apiKey);
  }
);

exports.deleteImage = onRequest(function (req, res) {
  return deleteImageModule.handler(req, res);
});
