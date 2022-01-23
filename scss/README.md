# CSS Framework for NCHIS

**Do not create any CSS files at the CSS folder of the project**

If you wish to create a CSS file, please create it under the `src` folder as a `scss` file.
Import your newly created scss file at `imports.scss` file.

Build the minified CSS file by `npm run gulp`, the minified css file will be created at the project folder. An unminified css file will be available at the `dist` folder.

## How to setup the CSS framework

* If you don't have NodeJS download and install NodeJs from - https://nodejs.org
* Install the dependancies `npm install`
* Compile the scss to css file by running `npm run gulp`

## SCSS

The CSS is written in `scss` preprocessor. You can include regular CSS rules at any `.scss` or you can write SCSS which will be processed in to regular CSS.

SCSS will reduce the number of CSS rules you will have to write, plus have benefits of variables, mathematical operations etc.

SCSS documentation available at - https://sass-lang.com/documentation



