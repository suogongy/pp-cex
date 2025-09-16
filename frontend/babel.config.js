module.exports = {
  presets: [
    ['next/babel', {
      'preset-env': {
        targets: {
          browsers: ['last 2 versions', 'not dead', 'not ie 11'],
        },
      },
    }],
  ],
  plugins: [
    '@babel/plugin-proposal-class-properties',
    '@babel/plugin-proposal-private-methods',
    '@babel/plugin-proposal-private-property-in-object',
  ],
};