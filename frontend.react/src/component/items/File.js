import React, {Component} from "react";

export default class File extends Component {
    constructor(props) {
        super(props);
        this.name = props.file.name;
    }

    render() {
        return <div>
            <p>FILE: {this.name}</p>
        </div>
    }
}
