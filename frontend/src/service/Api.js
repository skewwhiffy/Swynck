import Config from '../config/Config';
import Logger from '../logging/Logger';
import urlJoin from "url-join";

export default class Api {
  logger = new Logger();
  baseUrl = Config.baseUrl || "/";

  getRequestUrl(relative) {
    return urlJoin(this.baseUrl, relative);
  }

  handleNotOk(response) {
    if (response.ok) return response;
    this.logger.error(response.statusText);
    throw Error(response.statusText);
  }

  getCurrentUser() {
    return fetch(this.getRequestUrl("/api/user/me"))
      .then(this.handleNotOk)
      .then(it => it.json())
  }
}