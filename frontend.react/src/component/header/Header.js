import React, {Component} from "react";
import Auth from "../auth/Auth";
import "./header.css";

export default class Header extends Component {
  render() {
    return (
      <div className="header">
        <div className="headerContent">
          <div className="headerLeft">
            Swync
          </div>
          <div className="headerRight">
            <Auth />
          </div>
        </div>
      </div>
    )
  }
}
