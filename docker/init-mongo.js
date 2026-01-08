db.createUser({
  user: 'payment_user',
  pwd: 'payment_password',
  roles: [
    {
      role: 'readWrite',
      db: 'payment_system'
    }
  ]
});

db.createCollection('bookings');
db.createCollection('payments');
db.createCollection('users');