var numberLength = 9; // NRIC fixed length
var randomMax = 9; // max random number to be generated
var firstChars = {
  'NRIC': ['S', 'T']
}
var lastChars = {
  'NRIC': ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'Z', 'J'],
}
var intMul = [2, 7, 6, 5, 4, 3, 2];

function generateSingle(nricType) {
  var random = '';
  var mulTotal = 0;
  var checkIndex = 0;

  random += firstChars['NRIC'][nricType];

  for (var i = 0; i < numberLength - 2; i++) {
    random += Math.floor((Math.random() * randomMax)).toString();
  }

  if (random[0] == 'T') {
    mulTotal = 4;
  }

  for (var i = 1; i < numberLength - 1; i++) {
    mulTotal += parseInt(random[i]) * intMul[i - 1];
  }

  checkIndex = 10 - mulTotal % 11;
  random += lastChars['NRIC'][checkIndex];
  return random;
}

function setNRIC(nricType) {
  document.getElementById('userName').value = generateSingle(nricType);
}