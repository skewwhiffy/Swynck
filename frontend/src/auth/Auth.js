import React, { Component } from 'react';
import Api from '../service/Api';

export default class Auth extends Component {
  api = new Api();

  constructor(props) {
    super(props);
    this.state = {
      loggedIn: false,
      loginRedirect: false
    }
  }

  componentDidMount() {
    const code = (new URL(window.location)).searchParams.get("code");
    if (code) {
      debugger;
      this
        .api
        .setAuthCode(code)
        .then(_ => window.redirect("/"));
      return
    }

    this
      .api
      .getCurrentUser()
      .then(it => {
        if (it.redirect) {
          this.setState({
            loginRedirect: it.redirect,
            loggedIn: false
          })
        }
        if (it.email) {
          this.setState({
            loggedIn: it.email,
            loginRedirect: false
          })
        }
      })
  }


  render() {
    return (
      <div>
        {
          this.state.loggedIn &&
          <p>Welcome { this.state.loggedIn }</p>
        }
        {
          this.state.loginRedirect &&
          <a href={ this.state.loginRedirect }>Login</a>
        }
      </div>
    )
  }
}