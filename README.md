# Event4K

### Why it exist
because I needed a quick multiplatform event bus library and couldn't find
something which respect: `"Do One Thing and Do It Well"`<br/>
(it does one thing but doing it well is to be tested)

features:
* only 3 methods and 3 overloads, `register`, `publish`, `consumeOnce`
* `RegisterHook` to deRegister, number of invokes, last value, awaitNextInvoke, etc...
* `publisher` will receive return value of registrations (bi-directional)
* multiplatform (**common**)
### Usage

```kotlin
val event4k = Event4k()

fun main() {
    
    val hook1 = event4k.register("key") { event: Type, hook: RegisterHook<String> ->
        // handle event and possibly send something for publisher
        // hook is passed for per invoke inspection
        "handled"
    }

    // type overload, key is computed from Type
    val hook2 = even4k.register { e: Type, _ ->
        // ...
    }

    //....

    // Map<RegisterHook, Any>
    val registersReturnMap = event4k.publish("key", Type("event"))
    // type overload
    val registersReturnMap1 = event4k.publish(Type("event"))


    val resultOfComputationBlocking = event4k.consumeOnce { e: Type, _ ->
        return doSomethingOn(e) // and deRegister
    }
}
```

### Download

maven:
```xml
<!--add jitpack-->
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>


<!--add dependency-->
<!--probably in common main-->
<dependencies>
    <dependency>
        <groupId>com.github.nort3x</groupId>
        <artifactId>event4k</artifactId>
        <version>VERSION</version>
    </dependency>
</dependencies>

```

gradle:
```groovy
// add jitpack
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}

// add dependency
// probably in commonMain
dependencies {
    implementation 'com.github.nort3x:event4k:VERSION'
}

```

gradle(kotlin):
```kotlin
allprojects {
    repositories {
        ...
        maven { url = uri("https://jitpack.io") }
    }
}

// add dependency
// probably in commonMain
dependencies {
    implementation("com.github.nort3x:event4k:VERSION")
}
```

### Licence 
**MIT**

Copyright 2023 , Human Ardaki

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

