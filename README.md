# plate
- PLATE is a temPLATE engine for Java. 
- To use this library you will need at least Java 17. 

Templates are represented by `views`. These views are `.html` files which have the
following format:

```html

<template>
    here goes your html code
</template>

<style>
  here goes your styling
</style>

<script>
  here goes your javascript
</script>
```

Your `style` and `script` will not be shown in the rendered HTML response but will be linked with `link` and `script`
tags within the `head` element (if you provided it in your template). These links will try to get the resources under
the path `/plate/css/VIEWNAME.css` and `/plate/js/VIEWNAME.js`. This library itself does not provide the endpoints to
your application, but you can use adapters to hook theses into it.

### Usage without adapters
To create a `View` you simply create a new `View` object. You pass in the path to your `View` html file and the locale 
from your client for translations.

```java
View view = new View("MyView.html", Locale.ENGLISH);
```

Afterwards you can use your view to generate the HTML with the `render` function or to stream your html directly 
onto a `PrintWriter` with `stream`. To all these methods you have to provide a `Model` which is where
you store the data you want to be rendered into your views templates.

```java 
Model model = new Model();
model.add("message", "Hello World");

String html = view.render(model);
// or
PrintWriter responseWriter = response.getWriter();
view.stream(responseWriter, model);
responseWriter.close();
```

To retrieve the `style` or `script` sections of your view you have to call `View::getCSS` or `View::getJavaScript`.
```java 
 String css = view.getCSS();
 String javaScript = view.getJavaScript();
```

If you are streaming HTML you might want to deliver some parts of your website before you go 
and fetch data for the rest of the page. To do so you can simply use a build in Java `Supplier` as the
data within your model. When this data is first needed it will automatically be resolved and the 
resulting value will be stored within the model in the `Supplier` place. This means Suppliers will only
resolve once. Beware that resolving Suppliers will block the painting thread because HTML is hierarchical 
and we have to ensure certain paint-ordering. But you can use this feature to your advantage to stream
certain parts of your page before you go and fetch heavy data.

```java
Model model = new Model();
Supplier<Integer> messageSupplier = () -> {
    try {
        Thread.sleep(1000);
    } catch (InterruptedException e) {
        throw new RuntimeException(e);
    }
    return "Hello World";
    };
model.add("message", messageSupplier);

PrintWriter responseWriter = response.getWriter();
view.stream(responseWriter, model);
responseWriter.close();
```

### Adapters:
- [Plate-Spring-Adapter](https://github.com/voelza/plate-spring-adapter)


# Concepts
Generally this library structures its templates are considers `views` which can be composed multiple
children views called `components`.  The file you provide when you create a view object is considered the 
`ROOT` view. All other subviews which were used to compose this root view are imported automatically
and are named `COMPONENTS`.

Everything within a component will be scoped to that component instance. So if you provide styling for a 
component you don't have to worry about style class names because they will all be scoped to the
component name. Similarly, you don't have to worry about scoping in JavaScript for variables or functions
because theses will be wrapped into setup functions for your component.

Each component is customizable by passing properties into them from the parent component. That way you
can define generic components and reuse them throughout your application.

Components will be stored and used in a way which makes them cacheable to improve performance. This is
already handled by this library. Although this does not comply to root views which you created with the
default `new View(...)` constructor. While the underlining component of your provided view was cached 
the view object itself (and especially the CSS and JavaScript you might want to access) was not cached
anywhere. You might want to handle that yourself, but alternatively you can use the `Plate::createView` 
function to create a view and the `Plate::getCSS` and `Plate::getJavaScript` functions to retrieve them.

# Syntax

The most important symbols in the PLATE syntax is the double dollar symbol `$$`. This indicates that
a value has to be injected here and needs to be evaluated. The values for your templates will be pulled from your
provided `Model` into the `views` `render` or `stream` method. 

There are multiple ways of injecting values into your templates.

## Text Templates
To inject text into your template you have to use the following sequence `$${EXPRESSION}`.
For example, if you provided a text within your model with the name 'message' you can inject it into the
template using `$${message}` like this:
```html
MyView.html
<template>
    <h1>$${message}</h1>
</template>
```
And you render it like this:
```java
View view = new View("MyView.html", Locale.ENGLISH);
Model model = new Model();
model.add("message", "Hello World");
view.render(model);
```
Will result in:
```html
<h1>Hello World</h1>
```

## Attribute Templates
To inject values into attributes of your template you have to add the prefix `$$` to their names.
For example, if you provided a text within your model with the name 'color' you can inject it into
like this:
```html
MyView.html
<template>
    <h1 $$style="'background-color:' + color">Hello World!</h1>
</template>
```
And you render it like this:
```java
View view = new View("MyView.html", Locale.ENGLISH);
Model model = new Model();
model.add("color", "green");
view.render(model);
```
Will result in:
```html
<h1 style="background-color:green">Hello World</h1>
```

## Conditional Rendering
This library provides a way to render certain sections of your template depending on your provided
model. To do so you have to the `Render` element within your template. You will have to provide an
`if` attribute to it in which you declare the condition expression which will be evaluated on each
render.
```html
MyView.html
<template>
    <Render if="price > 20">
        <div>The price is too high!</div>
    </Render>
    <Render if="price <= 20">
        <div>I'll take it!</div>
    </Render>
</template>
```
And you render it like this:
```java
View view = new View("MyView.html", Locale.ENGLISH);
Model model = new Model();
model.add("price", 18);
view.render(model);
```
Will result in:
```html
<div>I'll take it!</div>
```

But if you change the value of price to 21. It will render something different:
```java
View view = new View("MyView.html", Locale.ENGLISH);
Model model = new Model();
model.add("price", 21);
view.render(model);
```
Will result in:
```html
<div>The price is too high!</div>
```
## For Loops
To reuse a template for each element within a collection you can use the provided `ForEach` element.
You will have to provide two attributes to it. The `collection` attribute which provides a expression
which will be evaluated with your model and has to result in an iterable collection or array. You also 
have to provide a `element` attribute which value is the name of the element as a string which you
can use within your template.

```html
MyView.html
<template>
    <ul>
        <ForEach collection="students" element="student">
            <li>$${student.name()}</li>
        </ForEach>
    </ul>
</template>
```
You render it like this:
```java
public record Student(String name) { }
[...]
View view = new View("MyView.html", Locale.ENGLISH);
Model model = new Model();
model.add("students", List.of(
        new Student("paul"), 
        new Student("george"),
        new Student("ringo"),
        new Student("john")
        ));
view.render(model);
```
It will result in the following html:
```html
<ul>
    <li>paul</li>
    <li>george</li>
    <li>ringo</li>
    <li>john</li>
</ul>
```

Additionally, you have access to the current index in each render cycle using the `_index` expression
which will automatically added to your model. If we for example use the same render code with this
template:
```html
MyView.html
<template>
    <ul>
        <ForEach collection="students" element="student">
            <li>$${_index + ': ' + student.name()}</li>
        </ForEach>
    </ul>
</template>
```
It will result in this html:
```html
<ul>
    <li>0: paul</li>
    <li>1: george</li>
    <li>2: ringo</li>
    <li>3: john</li>
</ul>
```

## Injecting HTML
By default, every text you inject into your templates will be escaped to security reasons.
Sometimes you might want to generate HTML within your Java application and want to inject it into
your template. To do so you will have to use the provided `Unsafe` element which will take your
html code within its mandatory `html` attribute.
```html
MyView.html
<template>
    <div>
        <Unsafe html="htmlFromJava" />
    </div>
</template>
```
You render it like this:
```java
View view = new View("MyView.html", Locale.ENGLISH);
Model model = new Model();
model.add("htmlFromJava","<script>alert('hello!');</script>");
view.render(model);
```
It will result in the following html:
```html
<div>
    <script>alert('hello!');</script>
</div>
```

## Script
Each script part of a view is scoped to it, and you don't have to worry about function or variable names.
This is achieved by wrapping your code within a setup function which will execute as soon as the whole
DOM is available. 

Each setup function will be passed two parameters: 
- `element` which represents the top level element of your component. Beware: if you have multiple top level elements within your component your script will be executed for each element.
- `props` is an object which holds all the properties of your component which you flagged to use `inScript`. To learn more follow this link [Properties in JavaScript](###properties-in-javascript)


# Using Components
To use components within your view you will have to import them using the provided `import` element.

This element must have the `file` attribute which has a relative path from your view to the component
you want to use.

So if your file structure is like this:
```
MyView.html
counter/Counter.html
```
Your `import` `file` attribute must look like this `file="counter/Counter.html"`. 

If you use the `@` letter on the start of your path the component file path will be
resolved against the set path in `Plate::setTemplatesPath`. 

So if your file structure looks like this:
```
templates/MyView.html
templates/counter/Counter.html
templates/utils/UUID.html
```
And in `Counter.html` you want to import `UUID.html` you will have to set your templates path like this 
`Plate.setTemplates("templates");`and write your import like this `file="@/utils/UUID.html"`. This will
result the component resolver to look for the `UUID` component at `templates/utils/UUID.html` while it
is resolving `Counter.html`.

Additionally, you have to provide a `name` attribute which provides a name as a string which you use
within your template to place this component within your HTML like this:
```html
MyView.html
<import file="counter/Counter.html" name="TheCounter"/>
<template>
    <div>
        <h1>My View has a counter:</h1>
        <TheCounter></TheCounter>
    </div>
</template>
```

If we have the following code for the counter component:
```html
/counter/Counter.html
<template>
    <button>Count</button>
</template>
```
The rendered HTML will be this:
```html
<div>
    <h1>My View has a counter:</h1>
    <button>Count</button>
</div>
```

## Properties
To make components reusable you can pass properties to them. Properties have to be defined within
a component's code and have to be passed down from the using view to the component with attributes.
Properties have to be defined using the provided `Prop` element which has to have a `name` attribute.
Components only have access to properties from your model which were passed down from the parent.

So if our counter component has to have a start value you will have to define it like this:
```html
counter/Counter.html
<prop name="startValue" />
<template>
    $${startValue}
    <button>Count</button>
</template>
```

To pass down a property to a component you simply use its defined name with the prefix `$$` as an 
attribute and define an expression which represents the value of the property like this:
```html
MyView.html
<import file="counter/Counter.html" name="TheCounter"/>
<template>
    <div>
        <h1>My View has a counter:</h1>
        <TheCounter $$startValue="10"></TheCounter>
    </div>
</template>
```

This will result in the following rendered HTML:
```html
<div>
    <h1>My View has a counter:</h1>
    10
    <button>Count</button>
</div>
```

## Properties in JavaScript
When you define a property you can add the optional attribute `inScript` to make this also available within
your script. You can then access it by using the `props` object which will be available within your 
script section. 
```html
/counter/Counter.html
<prop name="startValue" inScript/>
<template>
    <div>
        <span id="countDisplay">$${startValue}</span>
        <button id="counter">Count</button>
    </div>
</template>

<script>
const countDisplay = element.querySelector("#countDisplay");
const counter = element.querySelector("#counter")

let count = props.startValue;
counter.addEventListener("click", () => {
    count++;
    countDisplay.textContent = `${count}`;
});
</script>
```

If you want to pass properties to your script on your root view you will have to define these with the
provided `Prop` element as well and flag them as `inScript`.

## Slots
Slots allow you to inject HTML from a using (parent) view into a component at predefined spots within
their template. Slots can be named by adding a `name` attribute to them. To fill them at usage you have
to define your HTML elements as children within your component usage within an element with the
slot name. If your slot has no name it will automatically be called `default`.
For the elements within your slot-fill you have access to all the child props and all the properties
of your model.

To make this easier to understand here is a component 'Card':
```html
Card.html
<template>
    <div class="card">
        <div class="card-header">
            This is the card header!
        </div>
        <div class="card-body">
            <slot />
        </div>
    </div>
</template>
```

Now when you use this component you will have to fill the slot within the card-body like this:
```html
MyComponent.html
<import file="Card.html" name="Card" />
<template>
    <main>
        <card>
            <default>
                <h1>This is the card Content</h1>
            </default>
        </card>
    </main>
</template>
```
This will result in the following rendered HTML:
```html
<main>
    <div class="card">
        <div class="card-header">
            This is the card header!
        </div>
        <div class="card-body">
            <h1>This is the card Content</h1>
        </div>
    </div>
</main>
```

If you name your slots you will have to fill them with their name as children of the component
usage. If you use more than one slot within a component you will have to give at them names. There
can only be one `default` slot!

In our example we might want to add a slot for the card header:
```html
Card.html
<template>
    <div class="card">
        <div class="card-header">
           <slot name="header" />
        </div>
        <div class="card-body">
            <slot name="body" />
        </div>
    </div>
</template>
```

Now when you use this component you will have to fill the slots within the card like this:
```html
MyComponent.html
<import file="Card.html" name="Card" />
<template>
    <main>
        <card>
            <header>
                <h1>This is the card header!</h1>
            </header>
            <body>
                <h1>This is the card Content</h1>
            </body>
        </card>
    </main>
</template>
```

This will result in the following rendered HTML:
```html
<main>
    <div class="card">
        <div class="card-header">
            <h1>This is the card header!</h1>
        </div>
        <div class="card-body">
            <h1>This is the card Content</h1>
        </div>
    </div>
</main>
```

## Building Component Libraries for PLATE
To do so simply build a jar and put your PLATE components under `src/main/resources/libs/YOUR_LIB_NAME`.
Afterwards they can be imported like this: `@/libs/YOUR_LIB_NAME/YOUR_COMPONENT.html`. 


# Internationalization
If you need internationalization for your views you can provide them by setting a supply function
to `Plate::setTranslations`. This function takes in a function which as to return a 
`Map<Locale, Map<String, String>>`. If you provided translations for a certain locale you can access
them within your view with this syntax: `##{key}`. If there was no translation for a given locale found
it will default to english.

For example, given you provided these translations:
```java
Plate.setTranslations(() -> {
    Map<String, String> english = new HashMap<>();
    english.put("welcome.text", "Hello World!");

     Map<String, String> german = new HashMap<>();
    german.put("welcome.text", "Hallo Welt!");

     Map<Locale, Map<String, String>> translations = new HashMap<>();
    translations.put(Locale.ENGLISH, english);
    translations.put(Locale.GERMAN, german);
    return translations;
});
```

Then you can use the translation with the key `welcome.text` within your template like this:
```html
<template>
    <h1>##{welcome.text}</h1>
</template>
```

This will be rendered into this into these versions depending on the client locale.

EN:
```html
<template>
    <h1>Hello World!</h1>
</template>
```

DE:
```html
<template>
    <h1>Hallo Welt!</h1>
</template>
```
