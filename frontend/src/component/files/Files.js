import React, {Component} from "react";
import Api from "../../service/Api";

export default class Files extends Component {
  api = new Api();

  constructor() {
    super();
    this.refreshFiles = this.refreshFiles.bind(this);

    this.state = {
      files: []
    };
  }

  componentDidMount() {
    this.refreshFiles();
  }

  refreshFiles() {
    this.api.getFiles();
  }

  render() {
    return <p>Hello world</p>
  }
}