export default class {
  getTab() {
    return new URL(window.location).pathname.split('/').filter(it => it)[0];
  }

  setTab(tab) {
    window.history.pushState({}, "", `/${tab}`);
  }
}
