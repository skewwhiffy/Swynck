import React, {Component} from "react";

export default class Folder extends Component {
    constructor(props) {
        super(props);
        this.name = props.folder.name;
    }

    render() {
        return <div>
            <p>FOLDER: {this.name}</p>
        </div>
    }
}