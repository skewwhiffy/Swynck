import React, {Component} from "react";
import Api from "../../service/Api";
import Folder from "./Folder";
import File from "./File";

export default class Items extends Component {
  api = new Api();

  constructor() {
    super();
    this.refreshFiles = this.refreshFiles.bind(this);

    this.state = {
      folders: [],
      files: []
    };
  }

  componentDidMount() {
    this.refreshFiles();
  }

  refreshFiles() {
    const compare = (a, b) => (a > b) ? 1 : (a === b) ? 0 : -1;
    const nameCompare = (a, b) => compare(a.name.toLowerCase(), b.name.toLowerCase());
    this.api.getItems()
        .then(it => this.setState({
          files: it.files.sort(nameCompare),
          folders: it.folders.sort(nameCompare)
        }));
  }

  render() {
    return <div>
      {this.state.folders.map(it => <Folder key={it.name} folder={it}/>)}
      {this.state.files.map(it => <File key={it.name} file={it}/>)}
    </div>
  }
}