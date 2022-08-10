Coding conventions
------------------

* to define components, prefer createReactClass rather than class (to avoid problems with binding of this)
* avoid ternary operator (condition?expression1:expression2)
* do not use ../ in imports (always go down)
* avoid ref
* define propTypes and use isRequired whenever possible


Graylog Components
------------------

* components/common/Select. Use clearable={false} to ensure there is always a value
