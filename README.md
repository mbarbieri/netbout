<img src="http://img.netbout.com/logo.svg" width="132px"/>

[![Made By Teamed.io](http://img.teamed.io/btn.svg)](http://www.teamed.io)

[![Build Status](https://travis-ci.org/yegor256/netbout.svg?branch=master)](https://travis-ci.org/yegor256/netbout)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.netbout/netbout/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.netbout/netbout)

Netbout.com is a communication platform that enables smoothless integration
of humans and software agents in a conversation-centered environment.

Try it at [www.netbout.com](http://www.netbout.com).

The original idea behind Netbout is explained in USPTO patent application [US 12/943,022](https://www.google.com/patents/US20120117164).

## Functionality

A user is able to login, using one of the following social/federated methods: Facebook, Google+ and Github.
A user is able to logout.

When user logs in for the first time, he must create a new "alias" inside Netbout. The system checks
the validity of the alias and makes sure each alias is unique in the entire system.

A user can start a new "bout", which is a conversation between a few users.

A user can post a message to a bout and read all other messages, posted by other users, in reverse chronological order (the most recent messages on the top).

A user can invite other users to the bout, knowing just their aliases.

A user can attach a file to a bout. Any attached file can be deleted. A user can download any attached file.

## How to test?

If you're a manual tester and want to contribute to a project, please
login to Netbout.com, create an account and do whatever you think is reasonable
to reveal [functional](http://en.wikipedia.org/wiki/Functional_requirement) 
and [non-functional](http://en.wikipedia.org/wiki/Non-functional_requirement) 
problems in the system. Each bug you
find, report in [a new Github issue](https://github.com/yegor256/netbout/issues/new).

Please, read these articles before starting to test and report bugs:
[Five Principles of Bug Tracking](http://www.yegor256.com/2014/11/24/principles-of-bug-tracking.html),
[Bugs Are Welcome](http://www.yegor256.com/2014/04/13/bugs-are-welcome.html),
and
[Wikipedia's Definition of a Software Bug Is Wrong](http://www.yegor256.com/2015/06/11/wikipedia-bug-definition.html).

## How to contribute?

If you're a software developer and want to contribute to 
the project, please fork the repository, make changes, and submit a pull request.
We promise to review your changes same day and apply to
the `master` branch, if they look correct.

Please run Maven (3.1 or higher!) build before submitting a pull request:

```
$ mvn clean install -Pqulice
```

Want to run it locally? Simple as that:

```
$ mvn clean install -Phit-refresh -Dport=8080
```

In a minute the site is ready at `http://localhost:8080`
