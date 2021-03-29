select f.date, count(f.code) as quote,count(fh.code) as highlights,count(fi.code) as info,count(fs.code) as shares,count(fv.code) as valuation from firm_eod_quote f, firm_eod_highlights fh, firm_eod_info fi, firm_eod_share_stats fs, firm_eod_valuation fv where
        f.date = fh.date and
        f.date = fs.date and
        f.date = fi.date and
        f. date = fv.date and
        f.code = fh.code and
        f.code = fs.code and
        f.code = fi.code and
        f. code = fv.code
group by f.date order by f.date asc
