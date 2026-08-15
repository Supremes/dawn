#!/usr/bin/env bash

set -euo pipefail

module_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
framework="${1:-all}"

case "$framework" in
  jpa)
    tests="SpringDataJpaFeaturesTest"
    ;;
  hibernate)
    tests="HibernateFeaturesTest"
    ;;
  mybatis-plus|mp)
    tests="MybatisPlusFeaturesTest"
    ;;
  all)
    tests="SpringDataJpaFeaturesTest,HibernateFeaturesTest,MybatisPlusFeaturesTest"
    ;;
  *)
    echo "用法: $0 [jpa|hibernate|mybatis-plus|all]" >&2
    exit 2
    ;;
esac

mvn -f "$module_dir/pom.xml" -Dtest="$tests" test
