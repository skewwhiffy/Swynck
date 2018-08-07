import React, { Component } from 'react';
import Api from '../service/Api';

export default class Auth extends Component {
  api = new Api();

  constructor(props) {
    super(props);
    this.state = {
      message: "Woo hoo"
    }
  }

  componentDidMount() {
    this
      .api
      .getCurrentUser()
      .then(it => {
        if (it.redirect) {
          debugger;
          window.location.replace(it.redirect);
          return
        }
        throw Error("Not implemented yet")
      })
  }


  render() {
    return (
      <div>
        <p>{this.state.message}</p>
      </div>
    )
  }
}