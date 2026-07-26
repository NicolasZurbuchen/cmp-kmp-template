module.exports = {
  extends: ['@commitlint/config-conventional'],
  rules: {
    'type-enum': [
      2,
      'always',
      [
        'build',
        'chore',
        'ci',
        'docs',
        'feat',
        'fix',
        'perf',
        'refactor',
        'revert',
        'style',
        'test',
      ],
    ],
    'scope-enum': [
      2,
      'always',
      [
        'sync',
        'network',
        'database',
        'di',
        'navigation',
        'theme',
        'common',
        'gradle',
        'deps',
        'feature-a',
        'feature-b',
        'pokemon-explorer',
      ],
    ],
    'scope-empty': [0],
    'scope-required-on-types': [2, 'always'],
  },
  plugins: [
    {
      rules: {
        'scope-required-on-types': ({type, scope}) => {
          const requiredTypes = ['feat', 'fix', 'refactor', 'build'];
          const isRequired = requiredTypes.includes(type);
          if (isRequired && !scope) {
            return [false, `scope is required for types: ${requiredTypes.join(', ')}` ];
          }
          return [true];
        },
      },
    },
  ],
};
