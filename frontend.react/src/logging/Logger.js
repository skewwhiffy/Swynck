import Config from "../config/Config";
import moment from "moment";

const logLevels = {
  Debug: 1,
  Info: 2,
  Warn: 3,
  Error: 4,
  Disaster: 5
};

export default class Logger {
  get logLevel() {
    const levelText = (Config.logLevel || "Debug").toLowerCase();
    const levelKey = Object
      .keys(logLevels)
      .find(it => it.toLowerCase() === levelText);
    return logLevels[levelKey];
  }

  getLevelText(level) {
    return Object
      .keys(logLevels)
      .find(it => logLevels[it] === level) || "Disaster";
  }

  debug(message) {
    this.log(logLevels.Debug, message);
  }

  info(message) {
    this.log(logLevels.Info, message);
  }

  warn(message) {
    this.log(logLevels.Warn, message);
  }

  error(message) {
    this.log(logLevels.Error, message);
  }

  disaster(message) {
    this.log(logLevels.Disaster, message);
  }

  log(level, message) {
    if (level >= this.logLevel) {
      console.log(`${moment().format("HH:mm:ss")} : ${this.getLevelText(level)} : ${message}`);
    }
  }
}
