# ``>Reprompt``
#### Exam preparation e-learning tool

![Build Status][logo] 
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

[logo]: https://travis-ci.com/aworton/RepromptServer.svg?token=YXYYZLRjrctLryxGJPeQ&branch=master

Reprompt is an e-learning tool for educators to manage, produce and create content packages for 
their students.

Packages are highly focused and include both learning content and assessment material which is 
administered using spaced repetition in order to maximise retention for a specific examination 
date.

#### Technology

Reprompt is built using a service oriented architecture as a single page client application 
written in Angular 4 and Typescript, and as a RESTful server API implemented in Scala 2.12.2 using 
the Play! framework 2.6, Slick 3.2 database query and access library and Silhouette 5 
authentication framework.
 
#### Testing

Testing is handled by Jasmine, Karma and Protractor in the client application and Scalatest for 
the server.

