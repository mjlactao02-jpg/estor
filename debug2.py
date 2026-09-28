import re

with open('C:\\Users\\samsung\\Desktop\\Android Project\\estor\\app\\src\\main\\java\\estor\\app\\PinActivity.java', 'r', encoding='utf-8') as f:
    content = f.read()

# Find checkFirstLaunch method
idx = content.find('private void checkFirstLaunch')
if idx > 0:
    # Find the next method or end
    next_method = content.find('    private void', idx + 1)
    if next_method > 0:
        print(content[idx:next_method])
    else:
        print(content[idx:idx+800])