const bcrypt = require('bcrypt');

const password = 'stag123!';

bcrypt.hash(password, 12)
  .then(hash => {
    console.log(hash);
  })
  .catch(error => {
    console.error(error);
  });