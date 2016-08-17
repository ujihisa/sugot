sed -i -e 's|myrepo|/opt/spigot/myrepo|' -e 's/)//' project.clj
echo '  :deploy-repositories [["localm2" {:url "file:///var/lib/jenkins/.m2/repository/" :sign-releases false}]])' >> project.clj
cat project.clj
lein do test, install, deploy localm2, cloverage -o cov --coveralls
curl -F 'json_file=@cov/coveralls.json' "$COVERALLS_URL"
