import Config from '../config/Config';
import urlJoin from "url-join";

export default class Api {
  baseUrl = Config.baseUrl || "/";

  getRequestUrl(relative) {
    return urlJoin(this.baseUrl, relative);
  }

  getCurrentUser() {
    return fetch(this.getRequestUrl("/api/ping"))
      .then(it => it.text())
  }
}