#!/bin/bash
# First, update the files to remove secrets
sed -i 's/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/YOUR_API_KEY_PLACEHOLDER/g' "NotificationService/src/main/java/org/note/notificationservice/config/SendGridConfig.java"
sed -i 's/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/YOUR_API_KEY_PLACEHOLDER/g' "NotificationService/src/main/resources/application.properties"
sed -i 's/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/YOUR_API_KEY_PLACEHOLDER/g' "src/main/java/org/note/notesapplication/Config/SendGridConfig.java"

# Commit these changes
git add .
git commit -m "Replace API keys with placeholders"

# Clean git history
git filter-branch --force --index-filter \
  "git ls-files -z | xargs -0 sed -i 's/YOUR_ACTUAL_API_KEY_HERE/YOUR_API_KEY_PLACEHOLDER/g'" \
  --prune-empty --tag-name-filter cat -- --all

# Clean up
git for-each-ref --format='delete %(refname)' refs/original | git update-ref --stdin
git reflog expire --expire=now --all
git gc --prune=now --aggressive

# Force push
git push --force