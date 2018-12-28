import React, {Component} from 'react';
import {Tabs, Tab} from "react-bootstrap";
import Header from "./component/header/Header";
import Items from "./component/items/Items";
import "./app.css";
import Music from "./component/music/Music";

export default class App extends Component {
    render() {
        return (
            <div className="App">
                <Header/>
                <div className="content">
                    <Tabs id="tabs" defaultActiveKey="files">
                        <Tab eventKey="files" title="Files">
                            <Items/>
                        </Tab>
                        <Tab eventKey="music" title="Music">
                            <Music/>
                        </Tab>
                    </Tabs>
                </div>
            </div>
        );
    }
}
