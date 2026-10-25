from pathlib import Path
import sys,re,sqlite3
from tree_sitter import Language, Parser
import tree_sitter_swift,tree_sitter_kotlin
errors=[]
for suffix,module in [('.swift',tree_sitter_swift),('.kt',tree_sitter_kotlin)]:
 parser=Parser(Language(module.language()))
 for p in Path('.').rglob('*'+suffix):
  tree=parser.parse(p.read_bytes())
  if tree.root_node.has_error:
   def walk(n):
    if n.type=='ERROR' or n.is_missing:errors.append((str(p),n.type,n.start_point,n.end_point))
    for c in n.children:walk(c)
   walk(tree.root_node)
print('Syntax parser findings:',errors)
# Execute the actual Room migration SQL against populated legacy tables.
db=sqlite3.connect(':memory:')
db.execute('CREATE TABLE business_profile (id INTEGER PRIMARY KEY, businessName TEXT, phone TEXT)')
db.execute('CREATE TABLE invoices (id TEXT PRIMARY KEY, invoiceNumber TEXT, businessName TEXT)')
db.execute("INSERT INTO business_profile VALUES (1, 'Legacy Co', '0501234567')")
db.execute("INSERT INTO invoices VALUES ('old', 'INV-0001', 'Legacy Co')")
s=Path('android/app/src/main/java/com/alanbadrain/invoicemini/data/AppDatabase.kt').read_text()
statements=re.findall(r'db.execSQL\("([^"]+)"\)',s)
assert len(statements)==9
for statement in statements:db.execute(statement)
assert db.execute('SELECT invoiceNumber,businessPhone,currencyCode,stampPath,signaturePath FROM invoices').fetchone()==('INV-0001',None,None,None,None)
assert db.execute('SELECT phone,taxNumber,countryCode,callingCode FROM business_profile').fetchone()==('0501234567',None,None,None)
db.execute("UPDATE invoices SET currencyCode='KWD', businessPhone='+971 501234567', stampPath='/image.png'")
assert db.execute('SELECT currencyCode FROM invoices').fetchone()[0]=='KWD'
print('Room migration SQL: 9 additive columns; existing records retained; new values round-trip: PASS')

if errors:sys.exit(1)
