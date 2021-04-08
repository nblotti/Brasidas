select f.date,
       count(f.code)  as quote,
       count(fh.code) as highlights,
       count(fi.code) as info,
       count(fs.code) as shares,
       count(fv.code) as valuation
from firm_eod_quote f
         left join firm_eod_highlights fh on
        f.date = fh.date and
        f.code = fh.code
         left join firm_eod_info fi on
        f.code = fi.code and
        f.date = fi.date
         left join firm_eod_share_stats fs on
        f.code = fs.code and
        f.date = fs.date
         left join firm_eod_valuation fv on
        f.code = fv.code and
        f.date = fv.date
group by f.date
order by f.date desc;
