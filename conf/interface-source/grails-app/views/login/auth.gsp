<%--
  Created by IntelliJ IDEA.
  User: luisalcantara
  Date: 2019-05-14
  Time: 09:48
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />
    </head>
    <body>
        <div id="login">
            <div class="inner">
                <asset:image id="logo" src="switchboard_logo.jpg"  />
                <hr />
                <form action="/login/authenticate" method="POST" id="loginForm" class="cssform" autocomplete="off">
                    <p>
                        <label for="username">Username:</label>
                        <input type="text" class="text_" name="username" id="username">
                    </p>
                    <p>
                        <label for="password">Password:</label>
                        <input type="password" class="text_" name="password" id="password">
                    </p>
                    <p id="remember_me_holder">
                        <input type="checkbox" class="chk" name="remember-me" checked id="remember_me">
                        <label for="remember_me">Remember me</label>
                    </p>
                    <p>
                        <input type="submit" id="submit" value="Login">
                    </p>
                </form>
            </div>
        </div>
        <script>
            (function() {
                document.forms['loginForm'].elements['username'].focus();
            })();
        </script>
    </body>
</html>