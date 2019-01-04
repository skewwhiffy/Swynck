import React, {Component} from "react";
import File from "../items/File";
import Api from "../../service/Api";

export default class Music extends Component {
    api = new Api();

    constructor(props) {
        super(props);
        this.refreshMusic = this.refreshMusic.bind(this);

        this.state = {
            files: []
        }
    }

    componentDidMount() {
        this.refreshMusic();
    }

    refreshMusic() {
        this.api.searchMusic()
            .then(it => this.setState({
                files: it.files
            }));
    }

    render() {
        return <div>
            <div>
                <audio controls>
                    <source src="hello.mpg" type="audio/mpeg"/>
                </audio>
            </div>
            <div>
                {this.state.files.map(it => <File key={it.name} file={it}/>)}
            </div>
        </div>
    }
}
