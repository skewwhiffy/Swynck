import Config from '../config/Config';
import Logger from '../logging/Logger';
import urlJoin from "url-join";

export default class Api {
  logger = new Logger();
  baseUrl = Config.baseUrl || "/";

  constructor() {
    this.handleNotOk = this.handleNotOk.bind(this);
  }

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

  getItems(pwd) {
    const url = ['api', 'items', ...pwd].join('/');
    return fetch(this.getRequestUrl(url))
      .then(this.handleNotOk)
      .then(it => it.json());
  }

  searchMusic() {
    return fetch(this.getRequestUrl("/api/music"))
      .then(this.handleNotOk)
      .then(it => it.json());
  }

  setAuthCode(code) {
    return fetch(this.getRequestUrl("/api/onedrive/authcode"),
      {
        method: "POST",
        body: JSON.stringify({
          authCode: code
        })
      })
      .then(this.handleNotOk)
  }
}
