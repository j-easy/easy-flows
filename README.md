***

<div align="center">
    <b><em>Easy Flows</em></b><br>
    The simple, stupid rules workflow engine for Java&trade;
</div>

<div align="center">

[![MIT license](http://img.shields.io/badge/license-MIT-brightgreen.svg?style=flat)](http://opensource.org/licenses/MIT)
[![Build Status](https://travis-ci.org/j-easy/easy-flows.svg?branch=master)](https://travis-ci.org/j-easy/easy-flows)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.jeasy/easy-flows/badge.svg?style=flat)](http://search.maven.org/#artifactdetails|org.jeasy|easy-flows|0.1|)
[![Javadoc](https://www.javadoc.io/badge/org.jeasy/easy-flows.svg)](http://www.javadoc.io/doc/org.jeasy/easy-flows)
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/j-easy/easy-flows)

</div>

***

## Latest news

* 30/06/2017: The first version of Easy Flows has been released! Go get it and write some flows! See release notes [here](https://github.com/j-easy/easy-flows/releases).

## What is Easy Flows?

Easy Flows is a workflow engine for Java. It provides simple APIs and building blocks to make it easy to create and run composable workflows.

A unit of work in Easy Flows is represented by the `Work` interface. A work flow is represented by the `WorkFlow` interface.
Easy Flows provides 4 implementations of the `WorkFlow` interface:

<p align="center">
    <img src="https://raw.githubusercontent.com/wiki/j-easy/easy-flows/images/easy-flows.png" width="70%">
</p>

Those are the only basic flows you need to know to start creating workflows with Easy Flows.
You don't need to learn a complex notation or concepts, just a few natural APIs that are easy to think about.

## How does it work?

First let's write some work:

```java
class PrintMessageWork implements Work {

    private String message;

    public PrintMessageWork(String message) {
        this.message = message;
    }

    public String getName() {
        return "print message work";
    }

    public WorkReport call() {
        System.out.println(message);
        return new DefaultWorkReport(WorkStatus.COMPLETED);
    }
}
```

This unit of work prints a given message to the standard output. Now let's suppose we want to create the following workflow:

1. print "foo" three times
2. then print "hello" and "world" in parallel
3. then if both "hello" and "world" have been successfully printed to the console, print "ok", otherwise print "nok"

This workflow can be illustrated as follows:

<p align="center">
    <img src="https://raw.githubusercontent.com/wiki/j-easy/easy-flows/images/easy-flows-example.png" width="70%">
</p>

* `flow1` is a `RepeatFlow` of `work1` which is printing "foo" three times
* `flow2` is a `ParallelFlow` of `work2` and `work3` which respectively print "hello" and "world" in parallel
* `flow3` is a `ConditionalFlow`. It first executes `flow2` (a workflow is also a work), then if `flow2` is completed, it executes `work4`, otherwise `work5` which respectively print "ok" and "nok"
* `flow4` is a `SequentialFlow`. It executes `flow1` then `flow3` in sequence.

With Easy Flows, this workflow can be implemented with the following snippet:

```java
PrintMessageWork work1 = new PrintMessageWork("foo");
PrintMessageWork work2 = new PrintMessageWork("hello");
PrintMessageWork work3 = new PrintMessageWork("world");
PrintMessageWork work4 = new PrintMessageWork("ok");
PrintMessageWork work5 = new PrintMessageWork("nok");

WorkFlow workflow = aNewSequentialFlow() // flow 4
        .execute(aNewRepeatFlow() // flow 1
                    .named("print foo 3 times")
                    .repeat(work1)
                    .times(3)
                    .build())
        .then(aNewConditionalFlow() // flow 3
                .execute(aNewParallelFlow() // flow 2
                            .named("print 'hello' and 'world' in parallel")
                            .execute(work2, work3)
                            .build())
                .when(WorkReportPredicate.COMPLETED)
                .then(work4)
                .otherwise(work5)
                .build())
        .build();

WorkFlowEngine workFlowEngine = aNewWorkFlowEngine().build();
WorkReport workReport = workFlowEngine.run(workflow);
```

This is not a very useful workflow, but just to give you an idea about how to write workflows with Easy Flows.
You can find more details about all of this in the [wiki](https://github.com/j-easy/easy-flows/wiki).

## Why Easy Flows?

Easy Flows was created because of the lack of a simple open source workflow engine that can orchestrate `Callable` Java objects.
Why every single workflow engine out there is trying to implement BPMN? There is nothing wrong with BPMN, but it is not easy
( [538 pages specification??](http://www.omg.org/spec/BPMN/2.0/PDF) ). Same thing for [BPEL](http://docs.oasis-open.org/wsbpel/2.0/OS/wsbpel-v2.0-OS.pdf)..

There are currently good workflow engines, but since they try to implement BPMN, they are not easy to use and are often misused.
Most of BPMN concepts are not essential to many applications, and building such applications on top of a heavy engine is not efficient.

Easy Flows tries to provide a simple alternative with natural APIs that are easy to think about along with basic flows that most small/medium sized applications would require. If your business process requires a A0 paper to be drawn down, Easy Flows is probably not the right choice for you.

## Contribution

You are welcome to contribute to the project with pull requests on GitHub.

If you found a bug or want to request a feature, please use the [issue tracker](https://github.com/j-easy/easy-flows/issues).

For any further question, you can use the [Gitter](https://gitter.im/j-easy/easy-flows) channel of the project.

## License

Easy Flows is released under the terms of the MIT license:

```
The MIT License (MIT)

Copyright (c) 2020 Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
```
