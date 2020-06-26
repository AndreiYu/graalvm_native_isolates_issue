'use strict';
let param = 0

console.log()

if (typeof graalArg !== 'undefined') {
    param = graalArg
}

if (typeof javaSolver === 'undefined') {
    console.error("javaSolver is not defined")
}

console.log(`Incoming param ${param}`);
let res = 0

res = javaSolver.solve(param);

console.log()

res