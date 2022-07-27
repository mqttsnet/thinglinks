
'use strict'

if (process.env.NODE_ENV === 'production') {
  module.exports = require('./prismeditor.cjs.production.min.js')
} else {
  module.exports = require('./prismeditor.cjs.development.js')
}
