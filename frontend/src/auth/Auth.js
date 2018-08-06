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
    this.api.getCurrentUser().then(it => console.log(it));
  }


  render() {
    return (
      <div>
        <p>{this.state.message}</p>
      </div>
    )
  }
}